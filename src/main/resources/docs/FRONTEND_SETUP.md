# Angular Frontend Setup

## Prerequisites

- Node.js 20 LTS or 22
- npm compatible with the installed Node.js version
- Angular dependencies installed from `LearnAngular/package-lock.json`

## Install and run

```powershell
cd D:\chandran\LearnAngular
npm install --legacy-peer-deps
npm start
```

The development API URL is configured in
`LearnAngular/src/environments/environment.ts`.

## Corporate certificate setup

This workstation currently reports `SELF_SIGNED_CERT_IN_CHAIN` for npm and
`PKIX path building failed` for Maven Central. Install the organisation's root
certificate and configure npm to trust that certificate:

```powershell
npm config set cafile "C:\path\to\organisation-root-ca.pem"
```

Do not solve this by permanently disabling npm SSL validation. Java must also
trust the same root certificate in the Java 17 trust store used by Maven.

After certificate configuration, verify:

```powershell
npm install --legacy-peer-deps
npm run build
npm test -- --watch=false
```

## Main routes

- `/dashboard/financial-dashboard`
- `/dashboard/finance/customers`
- `/dashboard/finance/loans`
- `/dashboard/finance/collections`
- `/dashboard/chits`
- `/dashboard/reports`
- `/dashboard/customer-profile`
- `/dashboard/settings`
- `/dashboard/audit`
- `/dashboard/user-roles`
