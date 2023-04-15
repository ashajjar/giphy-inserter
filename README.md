# giphy-inserter

Insert a Giphy Anywhere

# Build and Package Locally

1. `git clone https://github.com/ashajjar/giphy-inserter.git`
2. Add you Giphy API Key in [api.properties](https://github.com/ashajjar/giphy-inserter/blob/main/src/jvmMain/resources/api.properties#L1)
3. `./gradlew clean build packageDistributionForCurrentOS` This command will build a package compliant with your OS, once this is complete you can install the resulting package like you do usually :)
