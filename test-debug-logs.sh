#!/bin/bash
# Start the application in the background
java -jar build/libs/branch-app-demo-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
APP_PID=$!
echo "Started application with PID: $APP_PID"

# Wait for app to start
sleep 5

# Test the endpoint
echo "Making test request..."
curl -s http://localhost:8080/api/v1/users/octocat > /dev/null

# Wait a moment for logs
sleep 2

# Show debug logs
echo -e "\n=== Debug Logs from GitHubService ==="
grep "GitHubService" app.log | grep DEBUG

# Cleanup
kill $APP_PID
rm app.log
