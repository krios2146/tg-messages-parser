package dev.krios2146.messagecounter

import ujson.Value.Value
import upickle.default.*

case class Reaction(`type`: String, count: Integer, emoji: String)

case class TextEntity(`type`: String, text: String, documentId: Option[String])

case class TelegramMessage(id: Long, `type`: String, from: Option[String], textEntities: List[TextEntity], reactions: Option[List[Reaction]])

@main
def main(): Unit = {
  val resources = os.pwd / "src" / "main" / "resources"

  val messagesJson = resources / "result.json"

  val jokergesFolder = resources / "jokerges"
  val rzhombiksFolder = resources / "rzhombiks"
  val staregesFolder = resources / "stareges"

  val jsonValue = ujson.read(os.read(messagesJson))
  val messageValues = jsonValue("messages").arr.toList
  val messages = messageValues.map(mapToTelegramMessage)

  val jokerges = os.list(jokergesFolder).map(_.last).toList
  val rzhombiks = os.list(rzhombiksFolder).map(_.last).toList
  val stareges = os.list(staregesFolder).map(_.last).toList

  val top50UsersByMessages = messages
    .filter(_.`type`.equals("message")) // only messages
    .flatMap(_.from) // get not None user who sent the message
    .groupBy(identity) // group the same users
    .view
    .mapValues(_.size) // modify key to be count of users messages
    .toList // (user, messageCount) tuple list
    .sortBy(-_._2) // desc sort by count
    .take(50)
    .zipWithIndex
    .map { case ((user, messageCount), index) => (index + 1, user, messageCount) }
    .map((id, user, messages) => s"$id. $user - $messages")

  val top10UsersByJokergeUsage = topUsersByEmojis(messages, 10, jokerges)
  val top10UsersByRzhombiksUsage = topUsersByEmojis(messages, 10, rzhombiks)
  val top10UsersByStaregeUsage = topUsersByEmojis(messages, 10, stareges)

  val tractorMentions = countMentions(messages, "Трактор")
  val factoryMentions = countMentions(messages, "Завод")

  val top10Reactions = topReactions(messages, 10)

  println("Top 50 Users by messages:")
  top50UsersByMessages.foreach(println)
  println()
  println("Top 10 Users by Jokerge pack usage:")
  top10UsersByJokergeUsage.foreach(println)
  println()
  println("Top 10 Users by rzhombiks pack usage:")
  top10UsersByRzhombiksUsage.foreach(println)
  println()
  println("Top 10 Users by Starege usage:")
  top10UsersByStaregeUsage.foreach(println)
  println()
  println(s"Трактор mentioned: $tractorMentions")
  println(s"Завод mentioned: $factoryMentions")
  println()

  println("Top 10 Reactions:")
  top10Reactions.foreach(println)
}

def topReactions(messages: List[TelegramMessage], top: Integer): List[String] = {
  messages
    .flatMap(_.reactions)
    .flatten
    .groupBy(_.emoji)
    .view
    .mapValues(countEmojis)
    .toList
    .filter { case (_, count) => count > 0 }
    .sortBy(-_._2)
    .take(top)
    .zipWithIndex
    .map { case ((user, messageCount), index) => (index + 1, user, messageCount) }
    .map((top, reaction, count) => s"$top. $reaction - $count")
}

def countMentions(messages: List[TelegramMessage], mentioned: String): Integer = {
  messages
    .flatMap(_.textEntities)
    .filter(_.`type`.equals("plain"))
    .map(_.text)
    .count(containsAny(_, List(mentioned)))
}

def topUsersByEmojis(messages: List[TelegramMessage], top: Integer, emojis: List[String]): List[String] = {
  messages
    .filter(_.`type`.equals("message"))
    .groupBy(_.from)
    .view
    .mapValues(countEmojis(_, emojis))
    .toList
    .collect { case (Some(user), count) => (user, count) }
    .filter { case (_, count) => count > 0 }
    .sortBy(-_._2)
    .take(top)
    .zipWithIndex
    .map { case ((user, messageCount), index) => (index + 1, user, messageCount) }
    .map((id, user, messages) => s"$id. $user - $messages")
}

def containsAny(string: String, substrings: List[String]): Boolean = {
  substrings
    .map(_.toLowerCase())
    .exists(string.toLowerCase().contains(_))
}

def countEmojis(reactions: List[Reaction]): Integer = {
  reactions
    .map(_.count)
    .fold(0: Integer)(_ + _)
}

def countEmojis(messages: List[TelegramMessage], toCount: List[String]): Integer = {
  messages
    .flatMap(_.textEntities)
    .filter(_.`type`.equals("custom_emoji"))
    .flatMap(_.documentId)
    .count(containsAny(_, toCount))
}

def mapToTelegramMessage(messageValue: Value): TelegramMessage = {
  TelegramMessage(
    id = messageValue("id").num.toLong,
    `type` = messageValue("type").str,
    from = messageValue.obj.get("from").flatMap(_.strOpt),
    textEntities = messageValue("text_entities").arr.map(mapToTextEntity).toList,
    reactions = messageValue.obj.get("reactions").map(_.arr.map(mapToReaction).toList)
  )
}

def mapToReaction(reactionValue: Value): Reaction = {
  Reaction(
    `type` = reactionValue("type").str,
    count = reactionValue("count").num.toInt,
    emoji = reactionValue("emoji").str
  )
}

def mapToTextEntity(textEntityValue: Value): TextEntity = {
  TextEntity(
    `type` = textEntityValue("type").str,
    text = textEntityValue("text").str,
    documentId = textEntityValue.obj.get("document_id").flatMap(_.strOpt)
  )
}