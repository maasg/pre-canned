package com.netaporter.precanned

import akka.actor.Actor
import spray.http.{ StatusCodes, HttpRequest, HttpResponse }
import com.netaporter.precanned.HttpServerMock.{ ClearExpecations, PrecannedResponse }
import StatusCodes._
import spray.can.Http.{ Connected, Register }

object HttpServerMock {
  case class PrecannedResponse(expects: Expect, response: HttpResponse)
  case object ClearExpecations
}

class HttpServerMock extends Actor {

  var responses = Vector.empty[PrecannedResponse]

  def receive = {
    case p: PrecannedResponse =>
      responses :+= p

    case ClearExpecations =>
      responses = Vector.empty[PrecannedResponse]

    case Connected(_, _) =>
      sender ! Register(self)

    case h: HttpRequest =>
      responses.find(_.expects(h)) match {
        case Some(pcr) =>
          sender ! pcr.response
        case None =>
          sender ! HttpResponse(status = NotFound)
      }
  }
}
