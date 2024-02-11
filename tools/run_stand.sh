# Initialize
method=ann
repo_path=/home/user/IdeaProjects/IR-stand/
collection_path=collections/collection.with-preprocessed-ids.500k.tsv
queries_path=collections/queries.preprocessed.500k.tsv
qrels_path=collections/qrels.preprocessed.500k.tsv
is_need_to_index=false

# Rebuild
cd $repo_path || { echo "Failure"; exit 1; }
$repo_path/gradlew clean bootJar

if [ "$is_need_to_index" = true ] ; then
  # Remove previous
  rm -rf $repo_path/indexes/$method/
  rm -rf $repo_path/outputs/$method/

  # Index
  java -jar \
    -Dstand.app.method=$method \
    -Dstand.app.base-path=$repo_path \
    $repo_path/build/libs/stand-0.0.1-SNAPSHOT.jar \
    save-in-batch --with-id $collection_path
fi

# Search
java -jar \
  -Dstand.app.method=$method \
  -Dstand.app.base-path=$repo_path \
  $repo_path/build/libs/stand-0.0.1-SNAPSHOT.jar \
  search -f MS_MARCO $queries_path

# Get MRR
python3 tools/msmarco_eval.py $qrels_path outputs/$method/resultInMrrFormat.tsv
