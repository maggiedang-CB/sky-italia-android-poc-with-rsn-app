#!/bin/bash

set -e

#set the buildNumber to the total number of commits on current branch plus 100
commitCount=$(git rev-list HEAD --count)
buildNumber=$(($commitCount + 4200000))

#parse the branch name
branch=$(git rev-parse --abbrev-ref HEAD)

#pull the versionName from build.gradle
version=$(awk '/defaultConfig/,/$p/' app/build.gradle | grep versionName | head -1 | sed 's/.*versionName "\(.*\)"\.*/\1/')

if [ $branch == "master" ]; then

  #set the teamcity build number
  echo "##teamcity[buildNumber '$version-$buildNumber']"

  ./gradlew :app:clean :app:assembleProdRelease :app:testProdReleaseUnitTest

else

  #set the teamcity build number
  echo "##teamcity[buildNumber '$version-$branch-$buildNumber']"

  ./gradlew :app:clean :app:assembleDevRelease :app:testDevReleaseUnitTest

fi