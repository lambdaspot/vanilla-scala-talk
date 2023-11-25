ThisBuild / version      := Versions.Project
ThisBuild / scalaVersion := Versions.Scala
ThisBuild / resolvers += "jitpack" at "https://jitpack.io"
ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("snapshots")

lazy val root = (project in file("."))
  .settings(
    name := "vanilla-scala-talk"
  )

// For Scala 2 use https://github.com/scalalandio/chimney as an alternative to Ducktape
libraryDependencies ++= Seq(
  "com.softwaremill.sttp.client3"         %% "core"                    % Versions.SttpClient,
  "io.github.arainko"                     %% "ducktape"                % Versions.Ducktape,
  "com.beachape"                          %% "enumeratum"              % Versions.Enumeratum,
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"     % Versions.JsoniterScala,
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros"   % Versions.JsoniterScala % "provided",
  "com.softwaremill.macwire"              %% "macros"                  % Versions.Macwire       % "provided",
  "com.softwaremill.macwire"              %% "proxy"                   % Versions.Macwire,
  "com.github.lambdaspot"                  % "aws-lambda-scala-bridge" % Versions.AwsLambdaScalaBridge,
  "org.scalatest"                         %% "scalatest"               % Versions.ScalaTest     % Test,
  "com.softwaremill.quicklens"            %% "quicklens"               % Versions.Quicklens     % Test,
  "org.wiremock"                           % "wiremock"                % Versions.Wiremock      % Test,
  "org.mockito"                            % "mockito-core"            % Versions.Mockito       % Test,
  "io.github.iltotore"                    %% "iron"                    % Versions.Iron,
  "io.github.iltotore"                    %% "iron-jsoniter"           % Versions.Iron
)

// TODO: add assembly
// TODO: add wartremover
