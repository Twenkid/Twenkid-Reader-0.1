# Twenkid-Reader-0.1
(Feed) reader &amp; aggregator for Android in Java. 
Coded during a few days hacking challenge from scratch with no prior mobile-app development experience. Thanks to Ivan for the challeng - to build some basic "useful app". That one had a listview with text and icons, favorites marking, a simple cache for offline viewing, threading, whatever...  Developed with Eclipse.

Created in 2/2013
License: MIT (C) 2018 

The source address was hard-coded to: http://feeds.feedburner.com/Mobilecrunch 
(because typing of such addresses on a mobile or tablet is not awkward)

The web site parsing code was developed from scratch (not using an API or something) and I don't know if it fits the current layout.

See: private class DownloadWebPageTask extends AsyncTask<String, Void, String> { ... in MyListViewActivity.java


