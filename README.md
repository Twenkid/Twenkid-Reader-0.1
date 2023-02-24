# Twenkid-Reader-0.1
(Feed) reader &amp; aggregator for Android in Java. 
Developed in a hacking session of a few days from scratch with no prior mobile-app development experience. Thanks to Ivan <a href="https://github.com/ivandzheferov">ivandzheferov</a> for suggesting me that challenge - to build a sample basic "useful app". That one had a listview with labels and icons, favorites marking, thumbnails, a simple cache for offline viewing, threading, menu, progress bar, whatever... IDE: Eclipse.

Created in 2/2013

License: MIT (C) 2018 

* The source address was hard-coded to: http://feeds.feedburner.com/Mobilecrunch  (It shouldn't, in general.)
* The web site parsing code was developed from scratch (not using an API or something) and I don't know if it fits the current layout.
  See: private class DownloadWebPageTask extends AsyncTask<String, Void, String> { ... in MyListViewActivity.java




