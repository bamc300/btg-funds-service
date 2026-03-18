SELECT DISTINCT c.nombre, c.apellidos
FROM BTG.Cliente c
JOIN BTG.Inscripcion i
  ON i.idCliente = c.id
WHERE EXISTS (
  SELECT 1
  FROM BTG.Disponibilidad d
  JOIN BTG.Visitan v
    ON v.idCliente = c.id
   AND v.idSucursal = d.idSucursal
  WHERE d.idProducto = i.idProducto
)
AND NOT EXISTS (
  SELECT 1
  FROM BTG.Disponibilidad d2
  WHERE d2.idProducto = i.idProducto
    AND NOT EXISTS (
      SELECT 1
      FROM BTG.Visitan v2
      WHERE v2.idCliente = c.id
        AND v2.idSucursal = d2.idSucursal
    )
);
