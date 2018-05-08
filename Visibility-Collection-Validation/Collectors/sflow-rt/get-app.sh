#!/bin/bash

usage ()
{
  echo "Usage   : $0 <github-user> <github-project>"
  echo "Example : $0 sflow-rt dashboard-example"
  echo "Catalog : http://sflow-rt.com/download.php#applications"
  exit
}

if [ "$#" -ne 2 ]; then
  usage
fi
pushd `dirname $0`
cd app
if wget -O master.zip "http://github.com/$1/$2/archive/master.zip"; then
  unzip master.zip
  mv $2-master $2
  rm master.zip
elif curl -LOk "http://github.com/$1/$2/archive/master.zip"; then
  unzip master.zip
  mv $2-master $2
  rm master.zip
fi
popd
echo ""
echo "==================================="
echo "Restart sflow-rt to run application"
echo "==================================="
