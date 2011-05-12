require 'nexus_indexer'
require 'fileutils'

old_found = []
describe Gem::NexusIndexer do

  before :each do
    @basedir = File.expand_path(File.dirname(__FILE__)).sub(/src\/test\/spec/, "target/test-classes")

    Dir[File.join(@basedir, "*4.8*")].each do |f|
      FileUtils.rm(f)
    end
    FileUtils.rm_rf(File.join(@basedir, 'quick'))

    @tmpdir = File.join(@basedir, "tmp")
    Dir.tmpdir(@tmpdir)
    @indexer = Gem::NexusIndexer.new(@basedir)
  end

  after :each do
    FileUtils.rm_rf(@tmpdir) if File.exists? @tmpdir
  end

  it 'should index gems with deep directory scan' do
    @indexer.generate_index

    found = Dir[File.join(@basedir, 'quick', 'Marshal.4.8', '*rz')]
    found.size.should == 3
    found = found.collect do |path|
      File.basename(path)
    end

    found.should_not == old_found

    # remember the list for the next spec
    old_found = found

    Dir[File.join(@basedir, "*4.8*")].size.should == 6
  end
end


