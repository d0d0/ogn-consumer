import java.util.regex.Pattern

object Parser {
  val aprsPositionPattern: Pattern = Pattern.compile(PatternConstants.PATTERN_APRS_POSITION, Pattern.MULTILINE)
  val aprsStatusPattern: Pattern = Pattern.compile(PatternConstants.PATTERN_APRS_STATUS, Pattern.MULTILINE)
  val ognAircraftPattern: Pattern = Pattern.compile(PatternConstants.PATTERN_AIRCRAFT_BEACON)
  val ognReceiverPattern: Pattern = Pattern.compile(PatternConstants.PATTERN_RECEIVER_BEACON)

  def isAPRSStatus(line: String): Boolean = {
    val statusMatcher = aprsStatusPattern.matcher(line)
    if (statusMatcher.matches()) {
      val comment = statusMatcher.group("comment")
      val receiverMatcher = ognReceiverPattern.matcher(comment)
      receiverMatcher.matches
    } else {
      false
    }
  }

  def isAircraft(line: String): Boolean = {
    val positionMatcher = aprsPositionPattern.matcher(line)
    if (positionMatcher.matches()) {
      val comment = positionMatcher.group("comment")
      if (comment != null) {
        val aircraftMatcher = ognAircraftPattern.matcher(comment)
        aircraftMatcher.matches()
      } else {
        false
      }
    } else {
      false
    }
  }

  def isBeacon(line: String): Boolean = {
    val positionMatcher = aprsPositionPattern.matcher(line)
    if (positionMatcher.matches()) {
      val comment = positionMatcher.group("comment")
      val receiverMatcher = ognReceiverPattern.matcher(comment)
      comment == null || receiverMatcher.matches()
    } else {
      false
    }
  }
}
