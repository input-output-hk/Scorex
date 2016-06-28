name := "scorex-basics"

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++=
    Dependencies.serialization ++
    Dependencies.akka ++
    Dependencies.p2p ++
    Dependencies.db ++
    Dependencies.http ++
    Dependencies.testKit ++
    Dependencies.db ++
    Dependencies.logging ++ Seq(
      "org.consensusresearch" %% "scrypto" % "1.2.0-RC1",
      "commons-net" % "commons-net" % "3.+"
  )
