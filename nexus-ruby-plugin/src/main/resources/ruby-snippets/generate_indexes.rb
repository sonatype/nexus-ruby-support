require 'rubygems'
require 'rubygems/indexer'

indexer = Gem::Indexer.new @basedir

if @update then
  indexer.update_index
else
  indexer.generate_index
end
