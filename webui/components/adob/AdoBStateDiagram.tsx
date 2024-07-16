import { useGetAllAdobCaches } from "@/lib/byzzbench-client";
import React from "react";
import { GraphView } from "react-digraph";

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

export default function AdoBStateDiagram() {
  const { data } = useGetAllAdobCaches();

  const nodes =
    data?.data.map((cache) => ({
      id: cache.id,
      title: `${cache.cacheType}`,
      type: cache.cacheType ?? "Election",
    })) ?? [];

  // compute edges from 'parent' relations
  const edges =
    data?.data
      .filter((cache) => cache.parentId !== null)
      .map((cache) => ({
        source: cache.parentId,
        target: cache.id,
        type: "emptyEdge",
      })) ?? [];

  const edgesx = [
    { source: "1", target: "2", type: "Custom" },
    { source: "1", target: "3", type: "emptyEdge" },
    { source: "3", target: "4", type: "emptyEdge" },
    { source: "4", target: "5", type: "emptyEdge" },
  ];

  const NodeTypes = GraphConfig.NodeTypes;
  const NodeSubtypes = GraphConfig.NodeSubtypes;
  const EdgeTypes = GraphConfig.EdgeTypes;

  return (
    <div style={{ height: "800px", width: "100%" }}>
      <GraphView
        nodeKey={"id"}
        nodes={nodes}
        edges={edges}
        //selected={selected}
        nodeTypes={NodeTypes}
        nodeSubtypes={NodeSubtypes}
        layoutEngineType="HorizontalTree"
        edgeTypes={EdgeTypes}
        allowMultiselect={true} // true by default, set to false to disable multi select.
      />
    </div>
  );
}
