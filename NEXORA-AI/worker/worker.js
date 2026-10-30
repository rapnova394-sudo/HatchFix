const SYSTEM_PROMPT = [
  "Eres NEXORA AI, un asistente inteligente.",
  "Responde en español salvo que el usuario escriba en otro idioma.",
  "Sé claro, preciso y útil.",
  "Adapta tus respuestas a una pantalla pequeña de smartwatch.",
  "No inventes información."
].join(" ");

export default {
  async fetch(request, env) {
    const corsHeaders = {
      "Access-Control-Allow-Origin": env.ALLOWED_ORIGIN || "https://nexora.invalid",
      "Access-Control-Allow-Methods": "POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type, Authorization"
    };

    if (request.method === "OPTIONS") {
      return new Response(null, { headers: corsHeaders });
    }

    if (request.method !== "POST") {
      return Response.json(
        { error: "Método no permitido" },
        { status: 405, headers: corsHeaders }
      );
    }

    if (!env.GROQ_API_KEY || !env.NEXORA_TOKEN ||
        request.headers.get("Authorization") !== "Bearer " + env.NEXORA_TOKEN) {
      return Response.json(
        { error: "No autorizado" },
        { status: 401, headers: corsHeaders }
      );
    }

    try {
      const body = await request.json();

      if (typeof body.message !== "string" ||
          body.message.trim().length === 0 ||
          body.message.length > 1000) {
        return Response.json(
          { error: "Mensaje no válido (máximo 1000 caracteres)" },
          { status: 400, headers: corsHeaders }
        );
      }

      const upstream = await fetch(
        "https://api.groq.com/openai/v1/chat/completions",
        {
          method: "POST",
          headers: {
            "Authorization": "Bearer " + env.GROQ_API_KEY,
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            model: env.GROQ_MODEL || "llama-3.3-70b-versatile",
            messages: [
              { role: "system", content: SYSTEM_PROMPT },
              { role: "user", content: body.message.trim() }
            ],
            temperature: 0.4,
            max_tokens: 180
          })
        }
      );

      if (!upstream.ok) {
        return Response.json(
          { error: "El servicio de IA no está disponible" },
          { status: 502, headers: corsHeaders }
        );
      }

      const data = await upstream.json();
      const answer = data.choices?.[0]?.message?.content || "";

      return Response.json(
        { answer },
        { headers: { ...corsHeaders, "Cache-Control": "no-store" } }
      );
    } catch {
      return Response.json(
        { error: "Error interno del servidor" },
        { status: 500, headers: corsHeaders }
      );
    }
  }
};
