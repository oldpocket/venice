# This script takes the output from ant locale and copies the key and english 
# values into a file.
use strict;

my %engIndex = ();
my @locales = ("en", "zh", "sv", "pl", "it", "fr", "ca", "de");
my @localeDesc = ("English", "Chinese", "Swedish", "Polish", "Italian", "French", "Catalan", "German");

my $basePath = "src/nz/org/venice/util/Locale/";
my $localeEngPath = "venice_en.properties";

my %missing = ();
my @missingLocales = ();

my $outputBase = "-forgoogle3.txt";

my %noiseList = ();
$noiseList{"in"} = 1;
$noiseList{"Install"} = 1;

sub mychomp
{
    my $ref = $_[0];
    my $sep = $_[1];

    {
	local $/ = $sep;
	chomp $$ref;	
    }

}

sub buildEngIndex
{

    my $line;
    my @tokens;

    print "Generating English Index\n";

    open (LOC, "<$basePath/$localeEngPath") or die "Couldnt open english locate for reading\n";

    while ($line = <LOC>) {
	if ($line =~ m/\#/) {
	    next;
	}

	if ($line eq "") {
	    next;
	}
	
	@tokens = split "=", $line;
	&mychomp(\$tokens[0], " ");
       
	if ($tokens[0] eq "" ) {
	    next;
	}
	$engIndex{$tokens[0]} = $tokens[1];
    }

    close LOC;

}

sub buildFile
{
    my $locale = $_[0];
    my $filename = $locale . $outputBase;

    print "Generating: $filename\n";

    open (LOC, ">$filename") or die "Couldnt open $filename for writing\n";
    
    foreach my $key (keys %missing) {
	my @tokens = split ":", $key;

	if ($locale ne $tokens[0]) {
	    next;
	}
	my $engValue = $engIndex{$tokens[1]};
	
	print LOC "$tokens[1] = $engValue";
    }
    print LOC "\n";

    close LOC;
}


sub buildMissingList
{
    my @tokens;
    my $locale;
    my $key;
    my $val;
    my $newLocale;
    my $line;

    print "Generating list of missing translactions\n";

    while ($line = <STDIN>) {
	if ($line =~ m/Comparing/) {
	    @tokens = split " ", $line;

	    $locale = $tokens[2];	    	    
	    push @missingLocales, $locale;
	}
	
	if ($line =~ m/MISSING/) {
	    @tokens = split " ", $line;
	    $key = $tokens[2];
	
	    if (&noise($key)) {
		next;
	    }
    	    
	    $val = $locale . ":" . $key;
	    #die "val = $val\n";
	    $missing{$val} = "1";
	}
    }
    
}


#Ant locale Comparison incorrectly recognizes some lines as
#property values   
sub noise
{
    my $key = $_[0];

    if ($noiseList{$key}) {
	return 1;
    } else {
	return 0;
    }    
}

sub main
{
    &buildEngIndex;
    &buildMissingList;

    foreach my $locale (@missingLocales) {
	&buildFile($locale);
    }
}


&main;
