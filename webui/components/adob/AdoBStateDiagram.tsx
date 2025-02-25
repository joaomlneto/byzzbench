"use client";

import { useGetAllAdobCaches } from "@/lib/byzzbench-client";
import React, { useEffect, useMemo, useState } from "react";
import { GraphUtils, GraphView } from "react-digraph";

export const AdoBStateDiagram = () => {
  const [isClient, setIsClient] = useState(false);

  useEffect(() => {
    setIsClient(true);
  }, []);

  const GraphConfig = {
    NodeTypes: {
      empty: {
        // required to show empty nodes
        typeText: "None",
        shapeId: "#empty", // relates to the type property of a node
        shape: (
          <symbol viewBox="0 0 100 100" id="empty" key="0">
            <circle cx="50" cy="50" r="45"></circle>
          </symbol>
        ),
      },
      custom: {
        // required to show empty nodes
        typeText: "Custom",
        shapeId: "#custom", // relates to the type property of a node
        shape: (
          <symbol viewBox="0 0 100 50" id="custom" key="0">
            <ellipse cx="50" cy="25" rx="50" ry="25"></ellipse>
          </symbol>
        ),
      },
      Root: {
        // required to show empty nodes
        typeText: "Root",
        shapeId: "#root", // relates to the type property of a node
        shape: (
          <symbol viewBox="0 0 100 100" id="root" key="0">
            <circle cx="50" cy="50" r="25"></circle>
          </symbol>
        ),
      },
      Election: {
        typeText: "Election",
        shapeId: "#election",
        shape: (
          <symbol viewBox="0 0 100 100" id="election" key="0">
            <polygon points="50,0 100,25 100,75 50,100 0,75 0,25"></polygon>
          </symbol>
        ),
      },
      Method: {
        typeText: "Method",
        shapeId: "#method",
        shape: (
          <symbol viewBox="0 0 100 100" id="method" key="0">
            <circle cx="50" cy="50" r="50"></circle>
          </symbol>
        ),
      },
      Commit: {
        typeText: "Commit",
        shapeId: "#commit",
        // commits are squares
        shape: (
          <symbol viewBox="0 0 100 100" id="commit" key="0">
            <rect x="0" y="0" width="100" height="100"></rect>
          </symbol>
        ),
      },
      Timeout: {
        typeText: "Timeout",
        shapeId: "#timeout",
        // timeouts are isosceles trapezoids (lower side is smaller than upper side)
        shape: (
          <symbol viewBox="0 0 100 100" id="timeout" key="0">
            <polygon points="0,0 100,0 80,100 20,100"></polygon>
          </symbol>
        ),
      },
    },
    NodeSubtypes: {},
    EdgeTypes: {
      emptyEdge: {
        // required to show empty edges
        shapeId: "#emptyEdge",
        shape: (
          <symbol viewBox="0 0 50 50" id="emptyEdge" key="0">
            <circle cx="25" cy="25" r="8" fill="currentColor">
              {" "}
            </circle>
          </symbol>
        ),
      },
    },
  };

  const { data } = useGetAllAdobCaches();

  const nodes = useMemo(() => {
    return (
      data?.data.map((cache) => ({
        id: cache.id,
        title: `t=${cache.timestamp}`,
        type: cache.cacheType ?? "Election",
        timestamp: cache.timestamp,
        // @ts-ignore // FIXME: need to fix the type of cache
        leader: cache.leader,
        // @ts-ignore // FIXME: need to fix the type of cache
        voters: cache.voters,
        // @ts-ignore // FIXME: need to fix the type of cache
        method: cache.method,
        // @ts-ignore // FIXME: need to fix the type of cache
        supporters: cache.supporters,
      })) ?? []
    );
  }, [data?.data]);

  // compute edges from 'parent' relations
  const edges = useMemo(() => {
    return (
      data?.data
        .filter((cache) => cache.parentId !== null)
        .map((cache) => ({
          source: cache.parentId,
          target: cache.id,
          type: "emptyEdge",
        })) ?? []
    );
  }, [data?.data]);

  return (
    <div style={{ height: "800px", width: "100%" }}>
      {isClient && (
        <GraphView
          nodeKey={"id"}
          nodes={nodes}
          edges={edges}
          nodeTypes={GraphConfig.NodeTypes}
          nodeSubtypes={GraphConfig.NodeSubtypes}
          layoutEngineType="HorizontalTree"
          edgeTypes={GraphConfig.EdgeTypes}
          allowMultiselect={false}
          renderNodeText={(data) => {
            const className = GraphUtils.classNames("node-text");
            return (
              <text className={className} textAnchor="middle">
                <tspan opacity="0.5">{data.type}</tspan>
                {data.timestamp != null && (
                  <tspan x={0} dy={12} fontSize="10px">
                    t = {data.timestamp}
                  </tspan>
                )}
                {data.leader != null && (
                  <tspan x={0} dy={12} fontSize="10px">
                    leader = {data.leader}
                  </tspan>
                )}
                {data.voters != null && (
                  <tspan x={0} dy={12} fontSize="10px">
                    voters = {data.voters.join(", ")}
                  </tspan>
                )}
                {data.supporters != null && (
                  <tspan x={0} dy={12} fontSize="10px">
                    supporters = {data.supporters.join(", ")}
                  </tspan>
                )}
                {data.method != null && (
                  <tspan x={0} dy={16} fontSize="10px">
                    method = {data.method}
                  </tspan>
                )}
              </text>
            );
          }}
        />
      )}
    </div>
  );
};
