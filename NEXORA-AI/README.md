# NEXORA AI

Vela Quick App prototype for Xiaomi Smart Band 10.

## Watch app

This folder uses the Songbird / AIoT Toolkit project structure.

```bash
npm install
npm run build
npm run release
```

The Vela app package identifier is `com.nexora.ai` and the design width is set to 106 dp for the 212 px-wide Smart Band 10 display.

## Groq proxy

The `worker/` directory contains a Cloudflare Worker proxy. The Groq API key must never be embedded in the watch app or committed to Git.

Configure secrets in the Worker environment:

```bash
wrangler secret put GROQ_API_KEY
wrangler secret put NEXORA_TOKEN
```

Set a long random `NEXORA_TOKEN` and keep it out of the public repository. Do not put a permanent shared token in the RPK: the Android companion app should hold the credential and relay requests using Notify's supported Interconnect mechanism.

Deploy the Worker from the `worker/` directory after reviewing access control, rate limits, and the actual companion-app transport.

## Current status

- Watch UI prototype: included.
- Groq proxy: initial server-side implementation included.
- Notify / Android Interconnect bridge: not implemented yet.
- RPK build and real-device compatibility: must be verified in the AIoT Toolkit environment.
