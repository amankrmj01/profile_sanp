# Profile Snap 📊

A high-performance REST API service that aggregates user profile data from popular competitive programming platforms.
Currently supporting **HackerRank** and **LeetCode** with plans to expand to other platforms soon.

## ✨ Features

- 🚀 **Fast Profile Retrieval** - Get comprehensive user profiles from multiple platforms
- 💾 **Smart Caching** - Built-in caching system to reduce API response times
- 🔄 **Resilient Scraping** - Fault-tolerant data extraction with retry mechanisms
- 📈 **Contest History** - Track user performance across programming contests
- 📝 **Recent Submissions** - Fetch latest problem submissions with detailed metadata
- 🎯 **RESTful API** - Clean, intuitive REST endpoints
- ⚡ **Micronaut Framework** - Lightweight, fast startup times

## 🛠️ Tech Stack

- **Java** - Core programming language
- **Micronaut** - Modern JVM framework
- **Gradle** - Build automation
- **Web Scraping** - Data extraction from platforms
- **In-Memory Caching** - Fast data retrieval

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Gradle 7.x or higher

### Installation

1. Clone the repository:

```bash
git clone https://github.com/amankrmj01/profile_sanp
cd profile_snap
```

2. Build the project:

```bash
./gradlew build
```

3. Run the application:

```bash
./gradlew run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Base URL

```
http://localhost:8080
```

---

## 🔵 LeetCode Endpoints

### 1. Get User Profile

**Endpoint:** `GET /leetcode/user/{username}`

**Description:** Fetches comprehensive LeetCode user profile information including stats, rankings, and personal
details.

**Example Request:**

```bash
curl -X GET "http://localhost:8080/leetcode/user/john_doe"
```

**Example Response:**

```json
{
  "username": "john_doe",
  "githubUrl": "https://github.com/johndoe",
  "twitterUrl": "https://twitter.com/johndoe",
  "linkedinUrl": "https://linkedin.com/in/johndoe",
  "profile": {
    "userAvatar": "https://assets.leetcode.com/users/avatars/avatar_1234.png",
    "realName": "John Doe",
    "websites": [
      "https://johndoe.dev"
    ],
    "countryName": "United States",
    "company": "Tech Corp",
    "jobTitle": "Software Engineer",
    "skillTags": [
      "JavaScript",
      "Python",
      "Java"
    ],
    "school": "MIT",
    "aboutMe": "Passionate software developer",
    "postViewCount": 1500,
    "postViewCountDiff": 50,
    "reputation": 2500,
    "ranking": 12345,
    "reputationDiff": 100,
    "solutionCount": 150,
    "solutionCountDiff": 5,
    "categoryDiscussCount": 25,
    "categoryDiscussCountDiff": 2,
    "certificationLevel": "Advanced"
  },
  "submitStats": {
    "acSubmissionNum": [
      {
        "difficulty": "Easy",
        "count": 120,
        "submissions": 180
      },
      {
        "difficulty": "Medium",
        "count": 85,
        "submissions": 145
      },
      {
        "difficulty": "Hard",
        "count": 25,
        "submissions": 78
      }
    ],
    "totalSubmissionNum": [
      {
        "difficulty": "All",
        "count": 230,
        "submissions": 403
      }
    ]
  },
  "contestBadge": {
    "name": "Knight",
    "expired": false,
    "hoverText": "Ranking in top 25%",
    "icon": "https://assets.leetcode.com/static_assets/others/knight.png"
  }
}
```

### 2. Get Contest History

**Endpoint:** `GET /leetcode/user/{username}/contests`

**Description:** Retrieves user's contest participation history and rankings.

**Example Request:**

```bash
curl -X GET "http://localhost:8080/leetcode/user/john_doe/contests"
```

**Example Response:**

```json
{
  "userContestRanking": {
    "attendedContestsCount": 15,
    "rating": 1850.5,
    "globalRanking": 2500,
    "totalParticipants": 50000,
    "topPercentage": 5.0,
    "badge": {
      "name": "Knight"
    }
  },
  "userContestRankingHistory": [
    {
      "attended": true,
      "trendDirection": "UP",
      "problemsSolved": 3,
      "totalProblems": 4,
      "finishTimeInSeconds": 5400,
      "rating": 1850.5,
      "ranking": 1200,
      "contest": {
        "title": "Weekly Contest 350",
        "startTime": "2024-06-15T14:30:00Z"
      }
    },
    {
      "attended": true,
      "trendDirection": "DOWN",
      "problemsSolved": 2,
      "totalProblems": 4,
      "finishTimeInSeconds": 7200,
      "rating": 1820.3,
      "ranking": 2800,
      "contest": {
        "title": "Weekly Contest 349",
        "startTime": "2024-06-08T14:30:00Z"
      }
    }
  ]
}
```

### 3. Get Recent Submissions

**Endpoint:** `GET /leetcode/user/{username}/submissions`

**Description:** Fetches user's recent problem submissions with optional limit.

**Query Parameters:**

- `limit` (optional): Number of submissions to retrieve (default: 20)

**Example Request:**

```bash
curl -X GET "http://localhost:8080/leetcode/user/john_doe/submissions?limit=5"
```

**Example Response:**

```json
[
  {
    "id": "12345678",
    "title": "Two Sum",
    "titleSlug": "two-sum",
    "timestamp": "2024-06-15T10:30:00Z",
    "status": "10",
    "statusDisplay": "Accepted",
    "lang": "python3",
    "url": "/problems/two-sum/submissions/12345678/",
    "langName": "Python3",
    "runtime": "52 ms",
    "isPending": false,
    "memory": "16.4 MB",
    "hasNotes": false,
    "notes": "",
    "flagType": "",
    "frontendId": "1",
    "topicTags": [
      {
        "id": "array"
      },
      {
        "id": "hash-table"
      }
    ]
  },
  {
    "id": "12345679",
    "title": "Add Two Numbers",
    "titleSlug": "add-two-numbers",
    "timestamp": "2024-06-14T16:45:00Z",
    "status": "11",
    "statusDisplay": "Wrong Answer",
    "lang": "java",
    "url": "/problems/add-two-numbers/submissions/12345679/",
    "langName": "Java",
    "runtime": "N/A",
    "isPending": false,
    "memory": "N/A",
    "hasNotes": true,
    "notes": "Need to handle edge cases",
    "flagType": "",
    "frontendId": "2",
    "topicTags": [
      {
        "id": "linked-list"
      },
      {
        "id": "math"
      }
    ]
  }
]
```

---

## 🟠 HackerRank Endpoints

### 1. Get User Profile

**Endpoint:** `GET /hackerrank/{username}`

**Description:** Retrieves HackerRank user profile with basic information and achievements.

**Example Request:**

```bash
curl -X GET "http://localhost:8080/hackerrank/jane_smith"
```

**Example Response:**

```json
{
  "username": "jane_smith",
  "fullName": "Jane Smith",
  "rank": 5420,
  "problemsSolved": 187,
  "profilePictureUrl": "https://hr-avatars.s3.amazonaws.com/12345678-1234-1234-1234-123456789012",
  "badges": [
    "Problem Solving (Intermediate)",
    "Java (Basic)",
    "Python (Intermediate)",
    "SQL (Advanced)"
  ],
  "bio": "Software engineer passionate about algorithms and data structures"
}
```

---

## 🗄️ Cache Management Endpoints

### 1. Get Cache Statistics

**Endpoint:** `GET /cache/stats`

**Description:** Returns current cache statistics and performance metrics.

**Example Request:**

```bash
curl -X GET "http://localhost:8080/cache/stats"
```

**Example Response:**

```json
{
  "totalCachedItems": 150,
  "hitRate": 85.5,
  "missRate": 14.5,
  "evictionCount": 12,
  "averageLoadTime": "250ms"
}
```

### 2. Clear All Cache

**Endpoint:** `POST /cache/clear`

**Description:** Clears all cached data from memory.

**Example Request:**

```bash
curl -X POST "http://localhost:8080/cache/clear"
```

**Example Response:**

```json
"All cache data cleared successfully"
```

### 3. Cleanup Expired Entries

**Endpoint:** `POST /cache/cleanup`

**Description:** Removes expired cache entries to free up memory.

**Example Request:**

```bash
curl -X POST "http://localhost:8080/cache/cleanup"
```

**Example Response:**

```json
"Expired cache entries cleaned up successfully"
```

### 4. Cache Health Check

**Endpoint:** `GET /cache/health`

**Description:** Returns cache health status.

**Example Request:**

```bash
curl -X GET "http://localhost:8080/cache/health"
```

**Example Response:**

```json
"Cache is healthy - Total items: 150"
```

---

## 🚦 HTTP Status Codes

| Status Code | Description                                                    |
|-------------|----------------------------------------------------------------|
| 200         | Success                                                        |
| 400         | Bad Request - Invalid username or parameters                   |
| 404         | User not found                                                 |
| 500         | Internal Server Error - Scraping failed or service unavailable |
| 503         | Service Unavailable - Target platform is down                  |

## 🔧 Configuration

The application runs on port `8080` by default. You can modify the configuration in`src/main/resources/application.yml`:

```yaml
micronaut:
  application:
    name: profilesnap
  server:
    port: 8080
    ssl:
      enabled: false
```

## 📝 Caching Strategy

- **Cache Duration**: Profiles are cached for optimal performance
- **Cache Keys**: Based on username and request parameters
- **Memory Management**: Automatic cleanup of expired entries
- **Performance**: Significant reduction in response times for repeated requests

## 🛣️ Roadmap

- [ ] **GitHub** profile integration
- [ ] **Codeforces** support
- [ ] **CodeChef** integration
- [ ] **AtCoder** platform support
- [ ] **TopCoder** profile data
- [ ] Rate limiting
- [ ] Webhook notifications

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support, email support@profilesnap.dev or create an issue in the repository.

---

**Built with ❤️ using Micronaut Framework**
