# Test gRPC calls with grpcurl

# 1. Get User Profile
grpcurl -plaintext -d '{"username":"amankrmj01"}' localhost:9090 ProfileService/GetUserProfile

# 2. Get User Contest History
grpcurl -plaintext -d '{"username":"amankrmj01"}' localhost:9090 ProfileService/GetUserContestHistory

# 3. Get User Submissions (with limit)
grpcurl -plaintext -d '{"username":"amankrmj01","limit":10}' localhost:9090 ProfileService/GetUserSubmissions

# 4. Get HackerRank Profile
grpcurl -plaintext -d '{"username":"amankrmj01"}' localhost:9090 ProfileService/GetHackerRankProfile

# 5. Get LeetCode Profile
grpcurl -plaintext -d '{"username":"amankrmj01"}' localhost:9090 ProfileService/GetLeetCodeProfile

# List all available services
grpcurl -plaintext localhost:9090 list

# List methods for ProfileService
grpcurl -plaintext localhost:9090 list ProfileService
