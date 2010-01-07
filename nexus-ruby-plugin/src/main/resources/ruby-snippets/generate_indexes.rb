require 'rubygems'
require 'rubygems/indexer'
 
indexer = Gem::Indexer.new @basedir
indexer.generate_index
