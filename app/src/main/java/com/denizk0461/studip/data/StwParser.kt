package com.denizk0461.studip.data

import com.denizk0461.studip.db.AppRepository
import com.denizk0461.studip.model.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

/**
 * Parser class used for fetching and collecting canteen offers from the website of the
 * Studierendenwerk Bremen.
 */
class StwParser {

    // Unique primary key value for the date elements
    private var dateId = 0

    // Unique primary key value for the canteen elements
    private var canteenId = 0

    // Unique primary key value for the category elements
    private var categoryId = 0

    // Unique primary key value for the item elements
    private var itemId = 0

    /**
     * Parses through a list of canteen plans and saves them to persistent storage.
     *
     * @param onRefreshUpdate   action call for when an update on the status of the fetch is
     *                          available
     * @param onFinish          action call for when the fetch has finished
     */
    fun parse(onRefreshUpdate: (status: Int) -> Unit, onFinish: () -> Unit) {
        /*
         * Reset primary key values. This needs to be done in case this method is called multiple
         * times within the app's lifespan
         */
        dateId = 0
        canteenId = 0
        categoryId = 0
        itemId = 0

        /*
         * Create and populate a list of all the canteen plans that will be fetched.
         * TODO implement functionality that will allow for fetching multiple plans, if necessary
         */
        val links = mutableListOf<String>()
        if (true) { links.add(urlUniMensa) }
//        if (true) { links.add(urlCafeCentral) }
//        if (true) { links.add(urlNW1) }
//        if (true) { links.add(urlGW2) }
//        if (true) { links.add(urlHSBNeustadt) }
//        if (true) { links.add(urlHSBAirport) }
//        if (true) { links.add(urlHSBWerder) }
//        if (true) { links.add(urlHfK) }
//        if (true) { links.add(urlMensaBHV) }
//        if (true) { links.add(urlCafeBHV) }

        // Delete all previous entries and start afresh
        Dependencies.repo.nukeOffers()

        /*
         * Fetch offers from every canteen individually. This is done in sequence for
         * simplicity's sake.
         * TODO perhaps it can be assumed here that the next two weeks will always be available?
         *  Hence, the date fetch might be superfluous and a database insert call could be
         *  implemented here instead. Another option would be to set the conflict policy to IGNORE
         *  and move the dateId reset from parseFromPage() to parse().
         */
        links.forEach { link ->
            parseFromPage(link, Dependencies.repo)

            // TODO implement onRefresh(Int)
        }

        // Action call once all fetching activities have finished
        onFinish()
    }

    /**
     * Fetch and parse the plan of a specific canteen.
     *
     * @param url   link to the canteen to be scraped. Must be a subpage of stw-bremen.de
     * @param repo  reference to the app repository used to save the items to persistent storage
     */
    private fun parseFromPage(url: String, repo: AppRepository) {
        // Used to store a parsed date string without creating a new variable on every loop
        var date: String

        // Reset the primary key value for the date objects
        dateId = 0

        // Parse the HTML using Jsoup to traverse the document
        val doc = Jsoup.connect(url).get()

        // Save the canteen to persistent storage
        repo.insert(OfferCanteen(canteenId, doc.getElementsByClass("pane-title")[1].text()))

        // Iterate through each day for which offers are available
        doc.getElementsByClass("food-plan").forEach { dayPlan ->

            /*
             * Fetch the date for a given day. It will be in the following format:
             * 24. Apr
             * This value will be split between the day (24.) and the month (Apr)
             */
            val rawDate = doc.getElementsByClass("tabs")[0]
                .getElementsByClass("tab-date")[dateId].text().split(" ")

            /*
             * Parse the two elements into a single date. The month will be converted to a numeric
             * value. Example:
             * Input: 24. Apr
             * Output: 24.04.
             */
            date = "${rawDate[0]}${rawDate[1].monthToNumber()}."

            /*
             * Save the date to persistent storage.
             * TODO this will duplicate date entries for every canteen that is fetched. This is
             *  inefficient.
             */
            repo.insert(OfferDate(dateId, date))

            // Iterate through all categories offered on a given day
            dayPlan.getElementsByClass("food-category").forEach { category ->
                // Retrieve the category text
                val categoryTitle = category.getElementsByClass("category-name")[0].text()

                // Save the category to persistent storage
                repo.insert(OfferCategory(categoryId, dateId, canteenId, categoryTitle))

                // Iterate through all items in a category
                category
                    .getElementsByTag("tbody")[0]
                    .getElementsByTag("tr").forEach { element ->
                        // Retrieve the parent element holding the item's title and price
                        val tableRows = element.getElementsByTag("td")

                        // Retrieve the dietary preferences the item meets
                        val prefs = element
                            .getElementsByClass("field field-name-field-food-types")[0]

                        // Parse the preferences into a string for the database
                        val prefString = DietaryPrefObject(
                            isFair = prefs.isDietaryPreferenceMet(imageLinkPrefFair),
                            isFish = prefs.isDietaryPreferenceMet(imageLinkPrefFish),
                            isPoultry = prefs.isDietaryPreferenceMet(imageLinkPrefPoultry),
                            isLamb = prefs.isDietaryPreferenceMet(imageLinkPrefLamb),
                            isVital = prefs.isDietaryPreferenceMet(imageLinkPrefVital),
                            isBeef = prefs.isDietaryPreferenceMet(imageLinkPrefBeef),
                            isPork = prefs.isDietaryPreferenceMet(imageLinkPrefPork),
                            isVegan = prefs.isDietaryPreferenceMet(imageLinkPrefVegan),
                            isVegetarian = prefs.isDietaryPreferenceMet(imageLinkPrefVegetarian),
                            isGame = prefs.isDietaryPreferenceMet(imageLinkPrefGame),
                        ).deconstruct()

                        // Save the item to persistent storage
                        repo.insert(
                            OfferItem(
                                itemId,
                                categoryId,
                                title = tableRows[1].getFilteredText(),
                                price = tableRows.getTextOrEmpty(2),
                                prefString,
                            )
                        )

                        // Increment the item ID to avoid overriding
                        itemId += 1
                    }
                // Increment the category ID to avoid overriding
                categoryId += 1
            }
            // Increment the date ID to avoid overriding
            dateId += 1
        }
        // Increment the canteen ID to avoid overriding
        canteenId += 1
    }

    /**
     * Evaluates whether a dietary preference is met by checking if the link to an image can be
     * found in the HTML.
     *
     * @param preference    constraint that needs to be met
     * @return              whether it is met
     */
    private fun Element.isDietaryPreferenceMet(preference: String): Boolean =
        getElementsByAttributeValue("src", preference).isNotEmpty()

    /**
     * Processes certain character references into human-readable characters. Since the fetched HTML
     * contains unresolved symbols such as &amp;, they need to be replaced with their counterpart
     * (in this case, &).
     *
     * @return  the filtered string
     */
    private fun Element.getFilteredText(): String {
        // Retrieve the element's inner HTML and replace faulty characters
        var text = html()
            .replace("&amp;", "&")
            .replace("&gt;", ">")
            .replace("&lt;", "<")

        /*
         * The Studierendenwerk's website lists allergens, but they are invisible, rendering them
         * pointless to the website user. As of now, this information is discarded in the app.
         * TODO implement allergen functionality
         */
//        while (text.contains("<sup>")) {
//            text = text.substring(0 until text.indexOf("<sup>")) + text.substring(text.indexOf("</sup>")+6 until text.length)
//        }

        // Return the stripped and updated HTML string
        return text
    }

    /**
     * Retrieve the text of an element in a certain position if the element can be found. Else,
     * return an empty string.
     *
     * @param index position at which a child
     * @return      text content or, if no element is found, an empty string
     */
    private fun Elements.getTextOrEmpty(index: Int): String = try {
        get(index).text()
    } catch (e: java.lang.IndexOutOfBoundsException) {
        ""
    }

    /**
     * Converts a month text string to its numeric value. Example:
     * Input: Apr
     * Output: 04
     *
     * @return  the numeric value of the month
     */
    private fun String.monthToNumber(): String = when (this) {
        "Jan" -> "01"
        "Feb" -> "02"
        "Mar" -> "03"
        "Apr" -> "04"
        "Mai" -> "05"
        "Jun" -> "06"
        "Jul" -> "07"
        "Aug" -> "08"
        "Sep" -> "09"
        "Okt" -> "10"
        "Nov" -> "11"
        "Dez" -> "12"
        else -> "00" // shouldn't occur
    }

    // Image links to all dietary preferences used for checking whether a preference is met
    private val imageLinkPrefFair = "https://www.stw-bremen.de/sites/default/files/images/pictograms/at_small.png"
    private val imageLinkPrefFish = "https://www.stw-bremen.de/sites/default/files/images/pictograms/fisch.png"
    private val imageLinkPrefPoultry = "https://www.stw-bremen.de/sites/default/files/images/pictograms/geflugel.png"
    private val imageLinkPrefLamb = "https://www.stw-bremen.de/sites/default/files/images/pictograms/lamm.png"
    private val imageLinkPrefVital = "https://www.stw-bremen.de/sites/default/files/images/pictograms/mensa_vital.png"
    private val imageLinkPrefBeef = "https://www.stw-bremen.de/sites/default/files/images/pictograms/rindfleisch.png"
    private val imageLinkPrefPork = "https://www.stw-bremen.de/sites/default/files/images/pictograms/schwein.png"
    private val imageLinkPrefVegan = "https://www.stw-bremen.de/sites/default/files/images/pictograms/mensa_vegan.png"
    private val imageLinkPrefVegetarian = "https://www.stw-bremen.de/sites/default/files/images/pictograms/vegetarisch.png"
    private val imageLinkPrefGame = "https://www.stw-bremen.de/sites/default/files/images/pictograms/wild.png"

    // URLs to all canteens in Bremen and Bremerhaven managed by the Studierendenwerk Bremen
    private val urlUniMensa = "https://www.stw-bremen.de/de/mensa/uni-mensa"
    private val urlCafeCentral = "https://www.stw-bremen.de/de/mensa/cafe-central"
    private val urlNW1 = "https://www.stw-bremen.de/de/mensa/nw-1"
    private val urlGW2 = "https://www.stw-bremen.de/de/cafeteria/gw2"
//    private val urlGraz = "" // No link since there are only snacks on offer that are not listed online
    private val urlHSBNeustadt = "https://www.stw-bremen.de/de/mensa/neustadtswall"
    private val urlHSBWerder = "https://www.stw-bremen.de/de/mensa/werderstra%C3%9Fe"
    private val urlHSBAirport = "https://www.stw-bremen.de/de/mensa/airport"
    private val urlHfK = "https://www.stw-bremen.de/de/mensa/interimsmensa-hfk"
    private val urlMensaBHV = "https://www.stw-bremen.de/de/mensa/bremerhaven"
    private val urlCafeBHV = "https://www.stw-bremen.de/de/cafeteria/bremerhaven"
}