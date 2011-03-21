#!/bin/bash

ANDROID_DIR=$1
IMAGE_NAME=$2
IMAGE_SIZE=$3
INPUT_DIR=$4

# Exit if program previously executed exited abnormally.
#
# arg1 - context-specific error message to print 
function exit_on_error
{
	exit_code=$?

	if [ $exit_code -ne 0 ] 
	then
		printf '%s (exit code %d)\n' "$1" $exit_code
		exit $exit_code
	fi
}

if [ $# != 4 ] 
then
	echo 'Usage:  <Android SDK install dir> <image name> <image size> <input dir>' 
	echo 
	echo 'Create sdcard image for Android emulator that is filled with the'
	echo 'exact contents of the given input directory.  Note that user'
	echo 'will be prompted for password for loopback device operations.'
	echo 
	echo 'The copy operation from input dir to the sdcard image will'
	echo 'dereference symbolic links.  Therefore, one could create an'
	echo 'input directory that contains symbolic links to other directories'
	echo 'or files.'
	exit 1
fi

IMAGE_PATH=$HOME/$IMAGE_NAME.fat32
MOUNT_PATH=/media/$IMAGE_NAME

$ANDROID_DIR/tools/mksdcard $IMAGE_SIZE $IMAGE_PATH

exit_on_error 'mksdcard failure'

sudo mkdir -p $MOUNT_PATH

exit_on_error 'failed to make mount path'

sudo mount -o loop $IMAGE_PATH $MOUNT_PATH

exit_on_error 'failed to make mount sdcard image'

pushd $INPUT_DIR
sudo cp -H -r * $MOUNT_PATH
exit_on_error 'failed to copy files into sdcard image'
popd

sudo umount $MOUNT_PATH

exit_on_error 'failed to unmount sdcard image'

