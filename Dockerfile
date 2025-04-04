# Build stage for native image
FROM ghcr.io/graalvm/native-image:latest AS build

# Install Maven
RUN microdnf install -y maven

WORKDIR /app
COPY pom.xml .
# Download dependencies separately to leverage Docker caching
RUN mvn dependency:go-offline -B
COPY src src
# Build native image
RUN mvn -Pnative native:compile -DskipTests

# Runtime stage
FROM ubuntu:22.04

# Install JDK and JVM diagnostics tools
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk ca-certificates curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/jvm-diagnostics-mcp .

# Create a non-root user
RUN useradd -m appuser
USER appuser

# Set environment variables for the MCP server with STDIO transport
ENV SPRING_MAIN_BANNER_MODE=off
ENV LOGGING_PATTERN_CONSOLE=""
ENV LOGGING_FILE_NAME=/tmp/jvm-diagnostics-mcp.log

ENTRYPOINT ["/app/jvm-diagnostics-mcp"]