package com.denizk0461.studip.data

data class StudIPEvent(
    val title: String, // the name of the event
    val lecturer: Array<String>, // the lecturer(s)
    val room: String, // the room the event takes place in; assumes that the room doesn't change
    val day: Int, // 0 = Monday, 1 = Tuesday, 2 = Wednesday, 3 = Thursday, 4 = Friday
    val timeslotStart: Int, // 0 = 08:15, 1 = 10:15, 2 = 12:15, 3 = 14:15, 4 = 16:15, 5 = 18:15
    val timeslots: Int, // the amount of timeslots the course takes place over (1 timeslot = 90min)
) {

    private val timeSlotStartTimes: List<String> = listOf("08:15", "10:15", "12:15", "14:15", "16:15", "18:15")
    private val timeSlotEndTimes: List<String> = listOf("09:45", "11:45", "13:45", "15:45", "17:45", "19:45")

    val timeslot: String =
        "${timeSlotStartTimes[timeslotStart]} – ${timeSlotEndTimes[timeslots - 1]}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StudIPEvent

        if (title != other.title) return false
        if (!lecturer.contentEquals(other.lecturer)) return false

        return true
    }
    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + lecturer.contentHashCode()
        return result
    }
}
