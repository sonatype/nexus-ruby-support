require 'nexus/bundler_dependencies'
require 'minitest/spec'
require 'minitest/autorun'

describe Nexus::BundlerDependencies do

  let :resources_dir do
    File.expand_path( File.join( File.dirname( __FILE__ ), '..', 'resources', 'bundler' ) )
  end

  let :version_map do
    { 
      'railties' => ['3.0.20','3.2.11','3.1.10','3.0.19','3.0.18','3.1.9','3.2.10','3.2.9','3.2.9.rc3','3.2.9.rc2','3.2.9.rc1','3.0.6','3.2.8.rc1','3.0.11','3.0.0.rc','3.2.3','3.2.6','3.2.2','3.0.1','3.1.7','3.1.4.rc1','3.2.4.rc1','3.1.1.rc3','3.0.8.rc1','3.0.2','3.0.9','3.0.0.beta4','3.1.0.rc4','3.1.3','3.2.0.rc2','3.0.4.rc1','3.0.7.rc1','3.0.9.rc5','3.1.1.rc1','3.1.5.rc1','3.0.0.beta2','3.0.5','3.0.4','3.0.13.rc1','3.2.0.rc1','3.2.3.rc1','3.1.0.beta1','3.0.3','3.0.7.rc2','3.0.5.rc1','3.0.0','3.0.9.rc4','3.0.6.rc1','3.0.0.rc2','3.0.8.rc2','3.0.6.rc2','3.0.0.beta3','3.0.16','3.0.0.beta','3.0.7','3.1.0.rc1','3.0.12.rc1','3.0.14','3.0.15','3.1.2.rc2','3.1.0.rc6','3.2.3.rc2','3.1.0.rc5','3.0.17','3.1.0.rc8','3.2.2.rc1','3.1.6','3.2.4','3.1.5','3.1.0.rc2','3.1.8','3.2.7.rc1','3.2.7','3.2.8.rc2','3.2.8','3.0.9.rc1','3.0.12','3.1.2.rc1','3.2.1','3.0.10.rc1','3.1.0','3.1.2','3.0.8','3.0.9.rc3','3.0.13','3.2.5','3.2.0','3.1.0.rc3','3.1.1.rc2','3.0.8.rc4','3.1.4','3.1.1','3.0.10']
    }
  end

  #let( :cached_railties ){ File.join( cache_dir, 'r', 'railties' ) }

  subject do
    Nexus::BundlerDependencies.new( version_map )
  end

  before {  }

  it 'should create cached when some versions are missing' do
    #FileUtils.rm_f( cached_railties )

    specs = Dir[ File.join( resources_dir, 'railties*.rz' ) ]
    missing_versions = subject.add( 'railties', nil )

    expected_missing_versions = specs.collect do |f|
      File.basename( f ).gsub( /^.*-|.gemspec.rz$/, '' )
    end

    (missing_versions - expected_missing_versions).must_equal []
    subject.array.must_equal []
    cached = JSON.parse( subject.update( 'railties', nil, *specs ) )

    reference = JSON.load( File.read( File.join( resources_dir, 'railties' ) ) )
    (cached.keys - reference.keys).must_equal []
    cached.keys.each do |k|
      (cached[ k ] - reference[ k ]).must_equal []
    end

    ref = Marshal.load( File.read( File.join( resources_dir, 'railties.dump' ) ) )

    ref = ref.collect do |r|
      r[:dependencies].sort!
      r = r.to_a
      r.sort!
      r.to_s
    end
    result = subject.array.collect do |r| 
      r[:dependencies].sort!
      r = r.to_a
      r.sort!
      r.to_s
    end
    ( result - ref ).must_equal []
  end
  
  it 'should fail when updating non existing spec' do
    lambda { subject.update( 'railties', 'blabla.gemspec.rz' ) }.must_raise Errno::ENOENT
  end

  it 'should fail when updating spec with wrong gem name' do
    spec = File.join( resources_dir, 'do_mysql-0.10.12.gemspec.rz' )
    lambda { subject.update( 'railties', nil, spec ) }.must_raise RuntimeError
  end

  it 'should update spec with right gem name' do
    spec = File.join( resources_dir, 'do_mysql-0.10.12.gemspec.rz' )
    subject.update( 'do_mysql', nil, spec ).wont_be_nil
  end

end
