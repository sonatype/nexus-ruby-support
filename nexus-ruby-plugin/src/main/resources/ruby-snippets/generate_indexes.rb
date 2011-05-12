require 'nexus_indexer'

indexer = Gem::NexusIndexer.new @basedir

# do not understand why this is needed but does not work otherwise with jruby
# jruby can not move away files from the tmp directory
tmpdir = File.join(@basedir, "tmp")
begin
  Dir.tmpdir(tmpdir)

  if @update then
    indexer.update_index
  else
    indexer.generate_index
  end
ensure
  FileUtils.rm_rf(tmpdir) if tmpdir
end

