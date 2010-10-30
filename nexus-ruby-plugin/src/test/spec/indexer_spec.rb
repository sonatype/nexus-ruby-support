$LOAD_PATH << File.expand_path(File.join(__FILE__, '../../../main/resources'))
$LOAD_PATH << File.expand_path(File.join(__FILE__, '../../../main/resources/ruby-snippets'))

require 'nexus_indexer'
require 'nexus_lazy_indexer'
require 'fileutils'

old_found = []
[Gem::NexusIndexer, Gem::NexusLazyIndexer].each do |indexer_class|
  describe indexer_class do
    
    before :each do
      @dir = File.expand_path(File.dirname(__FILE__))
      
      Dir[File.join(@dir, "*4.8*")].each do |f|
        FileUtils.rm(f)
      end
      FileUtils.rm_rf(File.join(@dir, 'quick'))
    end
    
    it 'should index gems with deep directory scan' do
      indexer = indexer_class.new(@dir)
      indexer.generate_index
      
      found = Dir[File.join(@dir, 'quick', 'Marshal.4.8', '*rz')]
      found.size.should == 3
      found = found.collect do |path|
        File.basename(path)
      end

      found.should_not == old_found
      
      # remember the list for the next spec
      old_found = found

      Dir[File.join(@dir, "*4.8*")].size.should == 6
    end
  end
end

