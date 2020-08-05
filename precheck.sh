#!/usr/bin/env bash

sbt clean scalastyle coverage test component:test coverageReport
