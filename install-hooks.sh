#!/bin/bash

# Script to install git hooks for semantic versioning

echo "Installing git hooks for semantic versioning..."

# Create hooks directory if it doesn't exist
mkdir -p .git/hooks

# Copy hooks
cp git-hooks/commit-msg .git/hooks/
cp git-hooks/post-commit .git/hooks/
cp git-hooks/pre-push .git/hooks/

# Make hooks executable
chmod +x .git/hooks/commit-msg
chmod +x .git/hooks/post-commit
chmod +x .git/hooks/pre-push

echo "Hooks installed successfully!"
echo "Your commits will now be validated for semantic format."
echo "Version tags will be created automatically based on commit messages."
echo "You will be reminded to push tags when pushing commits."