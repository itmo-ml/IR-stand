# Initialize
method=bm25
base_path=/your/base/path/
collection_path=collections/collection.500k.tsv
queries_path=collections/queries.500k.tsv
qrels_path=collections/qrels.500k.tsv
is_need_to_index=true

# Rebuild
cd $base_path || { echo "Failure"; exit 1; }
$base_path/gradlew clean bootJar

if [ "$is_need_to_index" = true ] ; then
  # Remove previous
  rm -rf $base_path/indexes/$method/
  rm -rf $base_path/outputs/$method/

  # Index
  java -jar \
    -Dstand.app.method=$method \
    -Dstand.app.base-path=$base_path \
    $base_path/build/libs/stand-0.0.1-SNAPSHOT.jar \
    save-in-batch --with-id $collection_path
fi

# Search
java -jar \
  -Dstand.app.method=$method \
  -Dstand.app.base-path=$base_path \
  $base_path/build/libs/stand-0.0.1-SNAPSHOT.jar \
  search -f MS_MARCO $queries_path

# Get MRR
python3 tools/msmarco_eval.py $qrels_path outputs/$method/resultInMrrFormat.tsv
