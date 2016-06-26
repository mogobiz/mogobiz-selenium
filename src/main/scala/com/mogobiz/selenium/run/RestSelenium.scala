package com.mogobiz.selenium.run

import akka.actor.Props
import akka.io.IO
import spray.can.Http
import com.mogobiz.pay.config.{MogopayRoutes}
import com.mogobiz.run.config
import com.mogobiz.run.es.EmbeddedElasticSearchNode
import com.mogobiz.run.jobs.CleanCartJob
import com.mogobiz.run.config.MogobizRoutes
import com.mogobiz.system.{ActorSystemLocator, RoutedHttpService, BootedMogobizSystem}
import com.mogobiz.launch.run.Settings

/**
  * Created by yoannbaudy on 19/12/2014.
  */
object RestSelenium
    extends App
    with BootedMogobizSystem
    with MogobizRoutes
    with MogopayRoutes
    with EmbeddedElasticSearchNode {

  ActorSystemLocator(system)

  val p      = config.Settings.config.getString("selenium-path-data")
  val esNode = startES(p)
  sys.addShutdownHook({ stopES(esNode) })

  com.mogobiz.pay.jobs.ImportRatesJob.start(system)
  com.mogobiz.pay.jobs.ImportCountriesJob.start(system)
  com.mogobiz.pay.jobs.CleanAccountsJob.start(system)

  override val bootstrap = {
    super[MogopayRoutes].bootstrap()
    com.mogobiz.session.boot.DBInitializer()
    com.mogobiz.notify.boot.DBInitializer()
  }

  //init jobs
  //  CleanCartJob.start(system)

  override val routes = super[MogobizRoutes].routes ~ super[MogopayRoutes].routes

  override val routesServices = system.actorOf(Props(new RoutedHttpService(routes)))

  IO(Http)(system) ! Http.Bind(routesServices, interface = Settings.Interface, port = Settings.Port)

}
