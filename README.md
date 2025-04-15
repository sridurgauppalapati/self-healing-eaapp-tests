# Self-Healing Test Automation Framework

![Java](https://img.shields.io/badge/Java-17+-blue)
![Selenium](https://img.shields.io/badge/Selenium-4.10.0-brightgreen)
![TestNG](https://img.shields.io/badge/TestNG-7.8.0-red)
![Maven](https://img.shields.io/badge/Maven-3.8.1-yellow)

A cutting-edge test automation framework with self-healing capabilities that automatically repairs broken locators using AI (GPT-4).

## Key Features

✨ **Self-Healing Locators** - Automatically detects and fixes broken XPath/CSS selectors  
🤖 **AI-Powered Recovery** - Uses OpenAI GPT-4 to generate alternative locators  
📊 **Healing History** - Maintains a repository of healed locators for future runs  
🚦 **Smart Fallback** - Implements multi-locator strategy for maximum stability  
📝 **Detailed Reporting** - Comprehensive logs and test execution reports  

## Prerequisites

- Java 11+
- Maven 3.8+
- OpenAI API key (for locator healing)
- Chrome/Firefox browser

## Installation

1. Clone the repository:

git clone https://github.com/your-username/self-healing-eaapp-tests.git
cd self-healing-eaapp-tests

2. Configure your OpenAI API key:

export OPENAI_API_KEY="your-api-key-here"
Or add it to src/main/resources/config.properties

3. Project Structure

self-healing-eaapp-tests/
├── src/
│   ├── main/java/com/eaapp/
│   │   ├── core/               # Framework core logic
│   │   ├── locators/           # Locator repository
│   └── test/java/com/eaapp/
│       └── tests/              # Test classes
│   └── main/resource/com/eaapp/
├──     └── healed_locators.json        # Auto-generated healed locators
        └── config.properties
├── pom.xml                     # Maven configuration
└── testng.xml                  # TestNG suite configuration

4. Configuration
    Edit src/main/resources/config.properties:

# Application Under Test
app.url=http://eaapp.somee.com

# AI Configuration
openai.api.key=${OPENAI_API_KEY}
openai.model=gpt-4
openai.temperature=0.7

# Locator Strategy
fallback.strategy=AI_FIRST
max.healing.attempts=3

5. Running Tests
  Run all tests:
    mvn clean test

  Run specific test group:
    mvn test -Dgroups=login

6. Generate report:
    mvn surefire-report:report

**How Self-Healing Works**

Detection: Framework detects a NoSuchElementException
Recovery: Checks local healed_locators.json for alternatives. If none found, queries GPT-4 for new locators. Validates suggested locators
Persistence: Successful locators are saved for future runs

