WITH dups AS (
SELECT
	regexp_replace(f1.fullpath, '/[^/]+$'::text, ''::text) AS path1,
	f1.name,
	f1.size,
	f1.hash,
	regexp_replace(f2.fullpath, '/[^/]+$'::text, ''::text) AS path2
FROM
	files f1
JOIN
	files f2 ON f1.search_id = f2.search_id AND f1.hash = f2.hash AND f1.fullpath != f2.fullpath
WHERE
	f1.search_id = 'ScanFotiekNaDS') 
SELECT 
	path1,
	ROUND(SUM(size) / (1024*1024)) total_size_mb,
	COUNT(*) duplicate_count,
	path2
FROM 
	dups
WHERE
	path1 < path2
GROUP BY 
	path1, path2
ORDER BY
	total_size_mb DESC