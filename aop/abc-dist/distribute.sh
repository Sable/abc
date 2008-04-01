#!/bin/sh -e

if [ "$1" = "" ]
  then echo "Please specify a version number"
       exit 1
fi


rm -rf /srv/www/abc.comlab.ox.ac.uk/html/dists/$1
cp -a dists/$1 /srv/www/abc.comlab.ox.ac.uk/html/dists/

if [ "$2" = "release" ] ; then
   credits.pl < package/abc-$1/abc/CREDITS > /srv/www/abc.comlab.ox.ac.uk/html/people.shtml
fi

if [ "$3" = "deb" ] ; then
   rm -f /srv/www/abc.comlab.ox.ac.uk/apt/dists/abc/main/binary-all/abc_${1}_all.deb
   ln -s \
          /srv/www/abc.comlab.ox.ac.uk/html/dists/$1/files/debian/abc_${1}_all.deb \
          /srv/www/abc.comlab.ox.ac.uk/apt/dists/abc/main/binary-all/

   /srv/www/abc.comlab.ox.ac.uk/scripts/build-apt.sh abc main
fi

echo Done.
