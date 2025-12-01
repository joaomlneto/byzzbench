# syntax=docker/dockerfile:1.6
FROM node:24 AS nodejs-base

# Generate the OpenAPI docs and build the Java application with better caching
# Use a Gradle image that includes JDK 21 to align with runtime
FROM gradle:8-jdk21 AS java-builder

WORKDIR /app

# 1) Copy only build configuration to cache dependency resolution
COPY settings.gradle.kts ./
COPY gradle.properties ./
COPY gradlew ./
COPY gradle/ ./gradle/
COPY buildSrc/ ./buildSrc/
COPY simulator/build.gradle.kts simulator/
COPY utilities/build.gradle.kts utilities/

# Ensure Gradle wrapper is executable (in case host permissions are not preserved)
RUN chmod +x ./gradlew || true

# 2) Warm up Gradle (cacheable layer)
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew --no-daemon --stacktrace help

# 3) Now copy only sources (avoid copying the entire repo)
COPY simulator/src ./simulator/src
#COPY utilities/src ./utilities/src

# 4) Build artifacts
RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew --no-daemon generateOpenApiDocs

RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    --mount=type=cache,target=/home/gradle/.gradle/wrapper \
    ./gradlew --no-daemon build

# Export OpenAPI spec for the web UI build stage
RUN cp ./simulator/build/openapi.json /openapi.json

# Export Spring Boot fat JAR (copy to a stable path)
# Try common patterns; fall back to the first JAR if needed
RUN mkdir -p /simulator && \
    JAR_PATH=$(find ./simulator/build/libs -type f -name "*.jar" ! -name "*-sources.jar" ! -name "*-javadoc.jar" | grep -E 'simulator-.*-all\.jar$|simulator-.*-fat\.jar$|simulator-.*\.jar$' | head -n 1) && \
    cp "$JAR_PATH" /simulator-app.jar

# Install dependencies only when needed
FROM nodejs-base AS webui-deps
## Check https://github.com/nodejs/docker-node/tree/b4117f9333da4138b03a546ec926ef50a31506c3#nodealpine to understand why libc6-compat might be needed.
#RUN apk add --no-cache libc6-compat
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
