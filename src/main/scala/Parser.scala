import java.util.regex.Pattern

object Parser {
  val aprsPositionPattern: Pattern = Pattern.compile(PatternConstants.PATTERN_APRS_POSITION, Pattern.MULTILINE)
  val aprsStatusPattern: Pattern = Pattern.compile(PatternConstants.PATTERN_APRS_STATUS, Pattern.MULTILINE)
  val ognAircraftPattern: Pattern = Pattern.compile(PatternConstants.PATTERN_AIRCRAFT_BEACON)
  val ognReceiverPattern: Pattern = Pattern.compile(PatternConstants.PATTERN_RECEIVER_BEACON)

  val KNOTS_TO_MS = 0.5144 // ratio knots to m/s
  val KPH_TO_MS = 0.27778 // ratio kph to m/s
  val FEETS_TO_METER = 0.3048 // ratio feets to meter
  val FPM_TO_MS = FEETS_TO_METER / 60 // ratio fpm to m/s


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
      if (comment != null) {
        val receiverMatcher = ognReceiverPattern.matcher(comment)
        receiverMatcher.matches()
      } else {
        true
      }
    } else {
      false
    }
  }

  final case class AircraftData(id: String = "", longitude: Double = 0, latitude: Double = 0, track: Int = 0, groundSpeed: Double = -1.0, altitude: Double = -1.0)

  implicit def toAircraftData(s: String): AircraftData = {
    val positionMatcher = aprsPositionPattern.matcher(s)
    if (positionMatcher.matches()) {
      val comment = positionMatcher.group("comment")
      val aircraftMatcher = ognAircraftPattern.matcher(comment)

      if (aircraftMatcher.matches()) {
        val latitudeSign: Double = if (positionMatcher.group("latitudeSign") == "S") -1 else 1
        val latitude: Double = latitudeSign * (AprsUtils.dmsToDeg(positionMatcher.group("latitude").toDouble / 100) +
          (if (positionMatcher.group("posExtension") == null) 0 else positionMatcher.group("latitudeEnhancement").toDouble / 1000 / 60))

        val longitudeSign: Double = if (positionMatcher.group("longitudeSign") == "W") -1 else 1
        val longitude: Double = longitudeSign * (AprsUtils.dmsToDeg(positionMatcher.group("longitude").toDouble / 100) +
          (if (positionMatcher.group("posExtension") == null) 0 else positionMatcher.group("longitudeEnhancement").toDouble / 1000 / 60))
        val track = if (positionMatcher.group("course") == null) 0 else positionMatcher.group("course").toInt
        val groundSpeed = if (positionMatcher.group("groundSpeed") == null) -1.0 else positionMatcher.group("groundSpeed").toDouble * KNOTS_TO_MS / KPH_TO_MS
        val altitude = if (positionMatcher.group("altitude") == null) -1 else positionMatcher.group("altitude").toDouble * FEETS_TO_METER

        AircraftData(aircraftMatcher.group("id"), longitude, latitude, track, groundSpeed, altitude)
      } else {
        AircraftData()
      }
    } else {
      AircraftData()
    }
  }
}
