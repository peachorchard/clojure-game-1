#!/usr/bin/bash
set -e  # stop on any error

# NOTE: -fat.jar files are only needed for uber-jar

# copy native JOGL .jar files to a folder
# then call this file with the folder name and
# -fat.jar files will be generated and installed
# locally using mvn

# in the project.clj file reference the 
# locally installed dependencies (-fat.jar files)
# with (e.g. some-file-natives-fat.jar):
# [com.daleroyer/clojure.deps.some-file-natives-fat.jar "1.0.0" :native-prefix ""]

if [[ $# -eq 0 ]]; then
    echo "usage: arguments are paths to original jar files"
    exit -1
else
    for DIR in $@; do
        for FILE in $DIR/*.jar; do
            if [[ "$FILE" = "$DIR/*.jar" ]]; then
                printf "WARNING: $FILE has no .jar files\n"
            else
                ALL_JAR_FILES+=($FILE)
            fi
        done
    done

    for FILE in ${ALL_JAR_FILES[@]}; do
        printf "processing: $FILE\n"
        BASE_NAME=`echo $FILE | sed -n "s/\(.*\)\.jar$/\1/p"`
        FAT_JAR_NAME=$BASE_NAME"-fat.jar"
        printf "   -> making fat jar: $FAT_JAR_NAME\n"
        ./make_fat_jar.sh "$BASE_NAME"
        printf "   -> installing fat jar: $FAT_JAR_NAME\n"
        ./installlocaljar.sh "$FAT_JAR_NAME"
    done
fi
