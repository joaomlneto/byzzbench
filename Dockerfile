FROM node:22.21.0-alpine AS nodejs-base

# Generate the OpenAPI docs and build the Java application
FROM gradle:8 AS java-builder

WORKDIR /app
COPY . .

RUN ./gradlew generateOpenApiDocs
RUN ./gradlew build
RUN cp ./simulator/build/openapi.json /openapi.json

# Install dependencies only when needed
FROM nodejs-base AS webui-deps
# Check https://github.com/nodejs/docker-node/tree/b4117f9333da4138b03a546ec926ef50a31506c3#nodealpine to understand why libc6-compat might be needed.
RUN apk add --no-cache libc6-compat
WORKDIR /app

# Install dependencies based on the preferred package manager
COPY webui/package.json webui/yarn.lock* webui/package-lock.json* webui/pnpm-lock.yaml* ./
RUN \
  if [ -f yarn.lock ]; then yarn --frozen-lockfile; \
  elif [ -f package-lock.json ]; then npm ci; \
  elif [ -f pnpm-lock.yaml ]; then corepack enable pnpm && pnpm i --frozen-lockfile; \
  else echo "Lockfile not found." && exit 1; \
  fi


# Rebuild the source code only when needed
FROM nodejs-base AS webui-builder
WORKDIR /app
COPY --from=webui-deps /app/node_modules ./node_modules
COPY --from=java-builder /openapi.json ./openapi.json
COPY webui/. .

# Next.js collects completely anonymous telemetry data about general usage.
# Learn more here: https://nextjs.org/telemetry
# Uncomment the following line in case you want to disable telemetry during the build.
# ENV NEXT_TELEMETRY_DISABLED 1

RUN ls -lA

RUN \
if [ -f pnpm-lock.yaml ]; \
    then corepack enable pnpm; \
fi

RUN \
  if [ -f yarn.lock ]; then yarn run kubb:generate -- ./openapi.json; \
  elif [ -f package-lock.json ]; then npm run kubb:generate -- ./openapi.json; \
  elif [ -f pnpm-lock.yaml ]; then pnpm run kubb:generate -- ./openapi.json; \
  else echo "Lockfile not found." && exit 1; \
  fi

RUN \
  if [ -f yarn.lock ]; then yarn run build; \
  elif [ -f package-lock.json ]; then npm run build; \
  elif [ -f pnpm-lock.yaml ]; then corepack enable pnpm && pnpm run build; \
  else echo "Lockfile not found." && exit 1; \
  fi

# Production image, copy all the files and run next
FROM nodejs-base AS webui-runner
WORKDIR /app

ENV NODE_ENV production
# Uncomment the following line in case you want to disable telemetry during runtime.
# ENV NEXT_TELEMETRY_DISABLED 1

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=webui-builder /app/public ./public

# Set the correct permission for prerender cache
RUN mkdir .next
RUN chown nextjs:nodejs .next

# Automatically leverage output traces to reduce image size
# https://nextjs.org/docs/advanced-features/output-file-tracing
COPY --from=webui-builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=webui-builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

EXPOSE 3000

ENV PORT 3000

# server.js is created by next build from the standalone output
# https://nextjs.org/docs/pages/api-reference/next-config-js/output
CMD HOSTNAME="0.0.0.0" node server.js
