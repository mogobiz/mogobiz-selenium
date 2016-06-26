name := "mogobiz-selenium"

Revolver.settings

libraryDependencies in ThisBuild ++= Seq(
  "org.codehaus.groovy" % "groovy-all" % "2.3.5"
)

mainClass in Revolver.reStart := Some("com.mogobiz.selenium.run.RestSelenium")

