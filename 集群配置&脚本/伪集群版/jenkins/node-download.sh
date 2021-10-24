#!/bin/sh -e

NODE_FILENAME="node-v${NODE_VERSION}-linux-x64.tar.xz"

url=https://nodejs.org/dist/v${NODE_VERSION}/${NODE_FILENAME}

echo "Downloading maven from $url"
wget  -qO /tmp/${NODE_FILENAME}  ${url}
