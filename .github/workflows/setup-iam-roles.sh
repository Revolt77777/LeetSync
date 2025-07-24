#!/bin/bash

# LeetSync GitHub Actions IAM Roles Setup
# Account: 799016889337
# GitHub: Revolt77777/LeetSync

echo "Setting up IAM roles for GitHub Actions..."

# First, create the GitHub OIDC provider if it doesn't exist
echo "Creating GitHub OIDC provider..."
aws iam create-open-id-connect-provider \
    --url https://token.actions.githubusercontent.com \
    --client-id-list sts.amazonaws.com \
    --thumbprint-list 6938fd4d98bab03faadb97b34396831e3780aea1 \
    --thumbprint-list 1c58a3a8518e8759bf075b76b750d4f2df264fcd 2>/dev/null || echo "OIDC provider already exists"

# Create trust policy for GitHub OIDC
cat > github-trust-policy.json << 'EOF'
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Principal": {
                "Federated": "arn:aws:iam::799016889337:oidc-provider/token.actions.githubusercontent.com"
            },
            "Action": "sts:AssumeRoleWithWebIdentity",
            "Condition": {
                "StringEquals": {
                    "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
                },
                "StringLike": {
                    "token.actions.githubusercontent.com:sub": "repo:Revolt77777/LeetSync:*"
                }
            }
        }
    ]
}
EOF

echo "Creating GitHubActionsRole-Test..."
# Create the test role
aws iam create-role \
    --role-name GitHubActionsRole-Test \
    --assume-role-policy-document file://github-trust-policy.json \
    --description "GitHub Actions role for LeetSync test environment"

# Attach permissions for test environment
aws iam attach-role-policy \
    --role-name GitHubActionsRole-Test \
    --policy-arn arn:aws:iam::aws:policy/PowerUserAccess

echo "Creating GitHubActionsRole-Prod..."
# Create the production role
aws iam create-role \
    --role-name GitHubActionsRole-Prod \
    --assume-role-policy-document file://github-trust-policy.json \
    --description "GitHub Actions role for LeetSync production environment"

# Attach permissions for production environment
aws iam attach-role-policy \
    --role-name GitHubActionsRole-Prod \
    --policy-arn arn:aws:iam::aws:policy/PowerUserAccess

echo ""
echo "✅ IAM Roles created successfully!"
echo ""
echo "Role ARNs (save these for GitHub secrets):"
echo "AWS_ROLE_ARN_TEST: arn:aws:iam::799016889337:role/GitHubActionsRole-Test"
echo "AWS_ROLE_ARN_PROD: arn:aws:iam::799016889337:role/GitHubActionsRole-Prod"
echo ""
echo "Next steps:"
echo "1. Add these ARNs as GitHub repository secrets"
echo "2. Create GitHub environments (test and production)"

# Cleanup
rm github-trust-policy.json