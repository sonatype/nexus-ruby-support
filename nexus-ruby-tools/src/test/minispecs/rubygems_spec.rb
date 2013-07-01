require 'nexus/rubygems'
require 'minitest/spec'
require 'minitest/autorun'

describe Nexus::Rubygems do

  subject { Nexus::Rubygems.new }

  let( :a1java ) { [ 'a', '1', 'java' ] }
  let( :a2java ) { [ 'a', '2', 'java' ] }
  let( :a1 ) { ['a', '1', 'ruby' ] }
  let( :a2 ) { ['a', '2', 'ruby' ] }
  let( :b4 ) { ['b', '4', 'ruby' ] }

  it 'should take the latest version' do
    specs = [ a2, b4, a1 ]
    subject.send( :regenerate_latest, specs ).must_equal [ a2, b4 ]
  end

  it 'should take the latest version per platform' do
    specs = [ a1java, a2java, a2, b4 ]
    subject.send( :regenerate_latest, specs ).sort.must_equal [ a2java, a2, b4 ]
  end

  it 'should take the latest version one per platform' do
    specs = [ a1java, a2, b4 ]
    subject.send( :regenerate_latest, specs ).must_equal [ a1java, a2, b4 ]
  end

end
