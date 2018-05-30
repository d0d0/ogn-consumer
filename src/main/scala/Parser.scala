import java.util.regex.Pattern

object Parser {
  val aprsPositionPattern: Pattern = Pattern.compile(PatternConstants.PATTERN_APRS_POSITION, Pattern.MULTILINE)
  val aprsStatusPattern: Pattern = Pattern.compile(PatternConstants.PATTERN_APRS_STATUS, Pattern.MULTILINE)
  val ognAircraftPattern: Pattern = Pattern.compile(PatternConstants.PATTERN_AIRCRAFT_BEACON)
  val ognReceiverPattern: Pattern = Pattern.compile(PatternConstants.PATTERN_RECEIVER_BEACON)

  def parse(line: String): Unit = {
    val statusMatcher = aprsStatusPattern.matcher(line)
    val positionMatcher = aprsPositionPattern.matcher(line)
    //println(statusMatcher.matches(), positionMatcher.matches())
  }
}
