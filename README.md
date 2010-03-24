Nexus Ruby Support
==================

This repository contains components and Nexus plugins to enhance Nexus with Ruby support. Stay tuned!

Powered with JRuby.

Problems to solve:

 * Maven Version to Ruby Gems Version convert (hard)
 * Currently, we do Gems from Java. Maybe reuse Ruby Gems to do that?
 * Indexing should be enhanced. Ruby Gems indexer does publish proper indexes downstream, but no Nexus integration is added yet (Gems are not searchable).
 * Enhance the embedded Ruby class in Gems created out of Jar, resolve: conflicts, better naming, etc.

Biggest challenges (unsolved)
-----------------------------

* Enhance embedded ruby stub script.

* Using effective POMs in MavenArtifact to Gem conversion. This is currently unclear, since this would need embedding Maven into Nexus, but that is completely different story.

* Improve indexing (gem indexing). Currently, I see some problems since indexing is unable to deduce is "update" enough or full indexing is needed (right now, on our "lab server" indexing fails, since it would need full reindex, but update is tried).

* Integrate Gem indexing with Nexus Indexing.

Biggest challenges (solved)
-----------------------------

* The Versioning scheme, it is currently the blocker. My initial "scratch" idea is following:

 * will soon extract all the "unique versions" from Maven central (and will put it here as file)
 * we should come up with some good heuristics to process them at least up to 95%
 * the rest would be a "human input", the version converter component should have some input file, where to find "exceptions" (or something alike this)

DONE. (mkristian)

Have fun!  
~t~
