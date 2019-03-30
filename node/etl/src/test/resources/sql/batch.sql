SELECT
	d1.id AS targetId,
	d2.id AS esTargetId,
	d1. NAME AS tableName,
	d2.NAMESPACE AS indexName,
	d1.NAMESPACE as schemaName,
	d2.NAME as indexType
FROM
	DATA_MEDIA d1,
	DATA_MEDIA d2
WHERE
d1.NAMESPACE = 'all_xxgl_otter'
AND d2. NAME = 'udip'
AND d1. NAME = d2.NAMESPACE order by d2.NAMESPACE asc ;
