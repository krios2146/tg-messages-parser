package dev.krios2146.messagecounter

import ujson.Value.Value
import upickle.default.*

case class TelegramMessage(id: Long, `type`: String, from: Option[String])

@main
def main(): Unit = {
  val path = os.pwd / "src" / "main" / "resources" / "result.json"

  val jsonValue: Value = ujson.read(os.read(path))
  val messageValues: List[Value] = jsonValue("messages").arr.toList
  val messages = messageValues.map(mapToTelegramData)

  messages
    .filter(_.`type`.equals("message"))
    .flatMap(_.from)
    .groupBy(identity)
    .view
    .mapValues(_.size)
    .toList
    .sortBy(-_._2)
    .take(50)
    .zipWithIndex
    .map { case ((user, messageCount), index) => (index + 1, user, messageCount) }
    .foreach((id, user, messages) => println(s"$id. $user - $messages"))
}

def mapToTelegramData(messageValue: Value): TelegramMessage = {
  TelegramMessage(
    messageValue("id").num.toLong,
    messageValue("type").str,
    messageValue.obj.get("from").flatMap(_.strOpt)
  )
}