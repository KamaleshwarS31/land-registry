import React, { useMemo } from 'react';

interface WktPolygonViewerProps {
  wkt: string;
  className?: string;
  fillColor?: string;
  strokeColor?: string;
}

export const WktPolygonViewer: React.FC<WktPolygonViewerProps> = ({
  wkt,
  className = 'w-full h-48',
  fillColor = '#10b981',
  strokeColor = '#059669',
}) => {
  const points = useMemo(() => {
    try {
      if (!wkt || !wkt.toUpperCase().includes('POLYGON')) return [];
      const match = wkt.match(/\(\((.*?)\)\)/);
      if (!match) return [];
      const rawCoords = match[1].split(',');
      return rawCoords.map((c) => {
        const parts = c.trim().split(/\s+/);
        return { x: parseFloat(parts[0]), y: parseFloat(parts[1]) };
      });
    } catch (e) {
      return [];
    }
  }, [wkt]);

  const { svgPoints, viewBox } = useMemo(() => {
    if (points.length < 3) return { svgPoints: '', viewBox: '0 0 100 100' };

    let minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity;
    points.forEach((p) => {
      if (p.x < minX) minX = p.x;
      if (p.x > maxX) maxX = p.x;
      if (p.y < minY) minY = p.y;
      if (p.y > maxY) maxY = p.y;
    });

    const paddingX = Math.max((maxX - minX) * 0.1, 5);
    const paddingY = Math.max((maxY - minY) * 0.1, 5);

    const vbMinX = minX - paddingX;
    const vbMinY = minY - paddingY;
    const vbW = Math.max(maxX - minX + paddingX * 2, 20);
    const vbH = Math.max(maxY - minY + paddingY * 2, 20);

    // In SVG, y axis points down, while geospatial coordinates point up
    const mapped = points.map((p) => {
      const flippedY = maxY - (p.y - minY);
      return `${p.x},${flippedY}`;
    }).join(' ');

    return {
      svgPoints: mapped,
      viewBox: `${vbMinX} ${vbMinY} ${vbW} ${vbH}`,
    };
  }, [points]);

  if (points.length < 3) {
    return (
      <div className={`flex items-center justify-center bg-slate-100 rounded-lg text-slate-400 text-xs ${className}`}>
        No valid polygon geometry
      </div>
    );
  }

  return (
    <div className={`relative bg-slate-900/5 rounded-lg border border-slate-200 overflow-hidden flex items-center justify-center p-2 ${className}`}>
      <svg
        viewBox={viewBox}
        className="w-full h-full max-h-full transition-transform hover:scale-105 duration-200"
      >
        <polygon
          points={svgPoints}
          fill={fillColor}
          fillOpacity={0.25}
          stroke={strokeColor}
          strokeWidth={2}
          strokeDasharray="4 2"
        />
        {points.slice(0, -1).map((p, idx) => (
          <circle
            key={idx}
            cx={p.x}
            cy={viewBox.split(' ')[3] ? parseFloat(viewBox.split(' ')[3]) - p.y : p.y}
            r={1.5}
            fill={strokeColor}
          />
        ))}
      </svg>
      <div className="absolute bottom-2 right-2 bg-slate-900/80 text-white text-[10px] px-2 py-0.5 rounded font-mono">
        {points.length - 1} Vertices
      </div>
    </div>
  );
};
