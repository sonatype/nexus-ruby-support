require 'rake'
require 'rake/testtask'
require 'bundler/setup'

task :default => [:test]

Rake::TestTask.new(:test) do |t|
  t.libs << "test"
  t.test_files = FileList['test/*_command_test.rb']
  t.verbose = true
end
