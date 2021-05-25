#This script takes all the FIXME values from the locales, replaces them with
#the english values. #The resulting file is then uploaded to Google Translate.

use strict;

my $locale;
my %engIndex = ();
my @locales = ("en", "zh", "sv", "pl", "it", "fr", "ca", "de");
my %localeFileRefs = ();
my %localeRefs = ();

my $basePath = "src/nz/org/venice/util/Locale/";
my $localePathKey = "venice_LANG.properties";

sub mychomp
{
    my $ref = $_[0];
    my $sep = $_[1];

    {
	local $/ = $sep;
	chomp $$ref;	
    }

}

sub buildFile
{
    my $locale = $_[0];
    my $path;
    my $line;
    my @lines = ();
    my @entries = ();
    my $debug = 0;

    $path = $basePath . $localePathKey;
    $path =~ s/LANG/$locale/;

    open (LOC , "<$path") or die "Couldnt open $path for reading";
    
    my $index = 0;
    while ($line = <LOC>) {
	chomp $line;
	$lines[$index] = $line;
	$index++;
    }
    close LOC;
    
    for ($index = 0; $index <= $#lines; $index++) {
	$line = $lines[$index];
	
	if ($line =~ m/\#/) {
	    next;
	}

	my $j = 1;
	my $entry = $line;
	my $nextLine = $lines[$index + $j];
	
	while ($nextLine !~ m/=/ &&
	    $nextLine !~ m/\#/ &&
	    $nextLine ne "") {
	   
	    $entry .= $nextLine;
	    $j++;
	    $nextLine = $lines[$index + $j];	    	    
	}
	#We joined $j-1 lines together
	#So we don't want to add them again
	if ($j >= 2) {
	    $index += $j - 1 
	}
	push @entries, $entry;	
    }
    $localeFileRefs{$locale} = \@entries;
}

sub buildIndex
{
    my $locale = $_[0];
    my $line;
    my @tokens;
    my %newLocale = ();
    my $path;
    my @entries;
    my $fileRef = $localeFileRefs{$locale};

    #$path = $basePath . $localePathKey;
    #$path =~ s/LANG/$locale/;

    print "building Index for $locale\n";
    
    #open (LOC, "<$path") or die "Couldnt open $path for reading\n";
    
    foreach $line (@$fileRef) {
	
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
	$newLocale{$tokens[0]} = $tokens[1];
	#print "|$tokens[0]|, |$tokens[1]|, $newLocale{$tokens[0]}\n";
    }

    $localeRefs{$locale} = \%newLocale;
   
    close LOC;    
}

sub dumpFIXME
{
    my $locale = $_[0];

    #Don't fix the English Locale - it's the reference
    if ($locale eq "en") {
	return;
    }

    open (LOC, ">$locale.forgoogle.txt") or die "Couldnt open $locale.forgoogle.txt\n";

    my $localeIndexRef = $localeRefs{$locale};
    my $engIndexRef = $localeRefs{$locales[0]};
    foreach my $key (keys %$localeIndexRef) {
	my $value = $localeIndexRef->{$key};
	
	if ($value =~ m/FIXME/) {
	    my $engValue = $engIndexRef->{$key};
	    print LOC "$key = $engValue\n";
	}
    }
    close LOC;
} 

sub main
{
    
    foreach my $locale (@locales) {
	&buildFile($locale);
	&buildIndex($locale);
	&dumpFIXME($locale);
    }
    
}

&main;
