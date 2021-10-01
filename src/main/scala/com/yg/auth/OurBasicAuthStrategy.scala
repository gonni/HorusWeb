package com.yg.auth

import org.scalatra.auth.strategy.{BasicAuthStrategy, BasicAuthSupport}
import org.scalatra.auth.{ScentryConfig, ScentrySupport}
import org.scalatra.ScalatraBase
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

class OurBasicAuthStrategy(protected override val app: ScalatraBase, realm: String)
  extends BasicAuthStrategy[User](app, realm) {

  protected def validate(userName: String, password: String)
                        (implicit request: HttpServletRequest, response: HttpServletResponse): Option[User] = {
    if (userName == "foo" && password == "bar") Some(User(userName))
    else None
  }

  protected def getUserId(user: User)
                         (implicit request: HttpServletRequest, response: HttpServletResponse): String = user.id
}

trait AuthenticationSupport extends ScentrySupport[User] with BasicAuthSupport[User] {
  self: ScalatraBase =>

  val realm = "Scalatra Basic Auth Example"

  protected def fromSession: PartialFunction[String, User] = {
    case id: String => User(id)
  }

  protected def toSession: PartialFunction[User, String] = {
    case usr: User => usr.id
  }

  protected val scentryConfig: ScentryConfiguration = new ScentryConfig {}.asInstanceOf[ScentryConfiguration]


  override protected def configureScentry(): Unit = {
    scentry.unauthenticated {
      scentry.strategies("Basic").unauthenticated()
    }
  }

  override protected def registerAuthStrategies(): Unit = {
    scentry.register("Basic", app => new OurBasicAuthStrategy(app, realm))
  }

}

case class User(id: String)